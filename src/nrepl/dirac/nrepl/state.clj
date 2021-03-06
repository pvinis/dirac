(ns dirac.nrepl.state)

; -- global state -----------------------------------------------------------------------------------------------------------

; here we keep a map of all bootstrapped Dirac sessions
(def session-descriptors (atom '()))

; here we maintain a list of in-progress jobs which want to be echo-ed back to joined-session
(def observed-jobs (atom {}))

(def sniffer->proxy (atom {}))

; -- session-specific state -------------------------------------------------------------------------------------------------

; we cannot pass nrepl-message info into all our functions,
; so we keep some global state around and various functions touch it at will

(def ^:dynamic *current-session* nil)
(def ^:dynamic *nrepl-message* nil)                                                                                           ; original incoming nrepl message "in-flight"

; -- dirac! eval state ------------------------------------------------------------------------------------------------------

(def ^:dynamic *reply!*)

(defn reply! [msg]
  (assert *reply!*)                                                                                                           ; should be set by binding wrapping eval in repl-eval!
  (*reply!* msg))

; -- convenience macros -----------------------------------------------------------------------------------------------------

(defmacro ensure-session [nrepl-message & body]
  `(do
     (assert (nil? *current-session*))
     (assert (nil? *nrepl-message*))
     (let [nrepl-message# ~nrepl-message
           session# (:session nrepl-message#)]
       (assert nrepl-message#)
       (assert session#)
       (binding [*current-session* session#
                 *nrepl-message* nrepl-message#]
         ~@body))))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn has-session? []
  (some? *current-session*))

(defn get-nrepl-message []
  (assert *nrepl-message*)
  *nrepl-message*)

(defn get-current-session []
  (assert *current-session*)
  *current-session*)

(defn get-current-session-if-avail []
  *current-session*)

(defn get-session-binding-value
  ([var] (get-session-binding-value *current-session* var))
  ([session var]
   (assert session)
   (get @session var)))

; implemented in session nREPL middleware
(defn get-session-id
  ([] (get-session-id *current-session*))
  ([session]
   (assert session)
   (-> session meta :id)))

(defn get-session-cljs-repl-env
  ([] (get-session-cljs-repl-env *current-session*))
  ([session]
   (assert session)
   (-> session meta ::cljs-repl-env)))

(defn set-session-cljs-repl-env!
  ([cljs-repl-env] (set-session-cljs-repl-env! *current-session* cljs-repl-env))
  ([session cljs-repl-env]
   (assert session)
   (alter-meta! session assoc ::cljs-repl-env cljs-repl-env)))

(defn dirac-session?
  ([] (dirac-session? *current-session*))
  ([session]
   (assert session)
   (some? (get-session-cljs-repl-env session))))

(defn get-session-selected-compiler
  ([] (get-session-selected-compiler *current-session*))
  ([session]
   (assert session)
   (-> session meta ::selected-compiler)))

(defn set-session-selected-compiler!
  ([selected-compiler] (set-session-selected-compiler! *current-session* selected-compiler))
  ([session selected-compiler]
   (assert session)
   (alter-meta! session assoc ::selected-compiler selected-compiler)))

(defn get-session-compiler-descriptors
  ([] (get-session-compiler-descriptors *current-session*))
  ([session]
   (assert session)
   (-> session meta ::compiler-descriptors)))

(defn set-session-compiler-descriptors!
  ([compiler-descriptors] (set-session-compiler-descriptors! *current-session* compiler-descriptors))
  ([session compiler-descriptors]
   (assert session)
   (alter-meta! session assoc ::compiler-descriptors compiler-descriptors)))

(defn get-session-cljs-repl-options
  ([] (get-session-cljs-repl-options *current-session*))
  ([session]
   (assert session)
   (-> session meta ::cljs-repl-options)))

(defn set-session-cljs-repl-options!
  ([cljs-repl-options] (set-session-cljs-repl-options! *current-session* cljs-repl-options))
  ([session cljs-repl-options]
   (assert session)
   (alter-meta! session assoc ::cljs-repl-options cljs-repl-options)))

(defn get-session-original-clj-ns
  ([] (get-session-original-clj-ns *current-session*))
  ([session]
   (assert session)
   (-> session meta ::original-clj-ns)))

(defn set-session-original-clj-ns!
  ([original-clj-ns] (set-session-original-clj-ns! *current-session* original-clj-ns))
  ([session original-clj-ns]
   (assert session)
   (alter-meta! session assoc ::original-clj-ns original-clj-ns)))

(defn get-session-cljs-ns
  ([] (get-session-cljs-ns *current-session*))
  ([session]
   (assert session)
   (-> session meta ::cljs-ns)))

(defn set-session-cljs-ns!
  ([cljs-ns] (set-session-cljs-ns! *current-session* cljs-ns))
  ([session cljs-ns]
   (assert session)
   (alter-meta! session assoc ::cljs-ns cljs-ns)))

(defn get-session-last-compiler-number
  ([] (get-session-last-compiler-number *current-session*))
  ([session]
   (assert session)
   (-> session meta ::last-compiler-number)))

(defn set-session-last-compiler-number!
  ([n] (set-session-last-compiler-number! *current-session* n))
  ([session n]
   (assert session)
   (alter-meta! session assoc ::last-compiler-number n)))
