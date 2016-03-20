(ns dirac.background.marion
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! chan put!]]
            [chromex.support :refer-macros [oget ocall oapply]]
            [chromex.logging :refer-macros [log info warn error]]
            [chromex.ext.tabs :as tabs]
            [chromex.ext.runtime :as runtime]
            [chromex.protocols :refer [post-message! get-sender get-name]]
            [dirac.background.action :as action]
            [dirac.background.state :as state]
            [dirac.utils :as utils]
            [cljs.reader :as reader]
            [dirac.background.helpers :as helpers]
            [dirac.options.model :as options]))

(defn automate-dirac-frontend! [message]
  (log "automate!" (pr-str message))
  (let [{:keys [action]} message
        connection-id (int (:connection-id message))]
    (if-let [connection (state/get-connection connection-id)]
      (helpers/automate-dirac-connection! connection-id action)
      (warn "dirac automation request for missing connection:" connection-id message
            "existing connections:" (state/get-connections)))))

(defn fire-synthetic-chrome-event! [message]
  (if-let [chrome-event-channel (state/get-chrome-event-channel)]
    (put! chrome-event-channel (:chrome-event message))
    (warn "no chrome event channel while receiving marion message" message)))

; -- marion event loop ------------------------------------------------------------------------------------------------------

(defn register-marion! [marion-port]
  (log "BACKGROUND: marion connected" (get-sender marion-port))
  (if (state/get-marion-port)
    (warn "overwriting previous marion port!"))
  (state/set-marion-port! marion-port))

(defn unregister-marion! []
  (log "BACKGROUND: marion disconnected")
  (state/set-marion-port! nil))

(defn process-marion-message [serialized-message]
  (log "process-marion-message" serialized-message)
  (let [message (reader/read-string serialized-message)]
    (case (:command message)
      :set-option (options/set-option! (:key message) (:value message))
      :reset-connection-id-counter (state/reset-connection-id-counter!)
      :fire-synthetic-chrome-event (fire-synthetic-chrome-event! message)
      :automate-dirac-frontend (automate-dirac-frontend! message))))

(defn run-marion-message-loop! [marion-port]
  (go-loop []
    (when-let [message (<! marion-port)]
      (process-marion-message message)
      (recur))
    (unregister-marion!)))

; -- marion client connection handling --------------------------------------------------------------------------------------

(defn handle-marion-client-connection! [marion-port]
  (register-marion! marion-port)
  (run-marion-message-loop! marion-port))

; -- marion feedback --------------------------------------------------------------------------------------------------------

(defn post-feedback-event! [text]
  (if-let [marion-port (state/get-marion-port)]
    (post-message! marion-port #js {:type "dirac-extension-feedback-event" :text text})))