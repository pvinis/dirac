// use goog.dependencies_ to list all relevant task namespaces
// we rely on the fact that we are running unoptimized clojurescript code here
// so all namespaces are present with original names

function pathToNamespace(path) {
  return path.replace(/_/g, "-").replace(/\//g, ".");
}

function getIndex(re) {
  var index = [];
  var container = goog.dependencies_.requires;
  for (var item in container) {
    if (container.hasOwnProperty(item)) {
      var m = item.match(re);
      if (m) {
        var path = m[1];
        index.push(pathToNamespace(path));
      }
    }
  }
  return index;
}

function genTaskList(runnerUrl, tasks) {
  var lines = ["<b>AVAILABLE TASKS:</b><ul>"];

  for (var i=0; i<tasks.length; i++) {
    var ns = tasks[i];
    var line = "<li><a href=\""+ runnerUrl +"?task="+ ns +"\">"+ns+"</a></li>";
    lines.push(line);
  }
  lines.push("</ul>");
  return lines.join("");
}

var tasks = getIndex(/tasks\/(.*)\.js/);
var tasksMarkup = genTaskList("runner.html", tasks);

var scenariosMarkup = "<b><a href=\"scenarios\">AVAILABLE SCENARIOS</a></b>";

document.body.innerHTML = [tasksMarkup, scenariosMarkup].join("<br>");