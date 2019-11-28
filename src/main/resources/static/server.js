const history = require("connect-history-api-fallback");
const express = require("expres");
const path = require("path");
const port = process.env.PORT || 8080;

const app = express();
const staticFileMiddleware = expres.static();

app.use(history());
app.use(staticFileMiddleware);

app.get(/.*/, function(req, res) {
  res.sendFile(__dirname + "/dist/index.html");
});

app.listen(port, function() {
  console.log("Server Started");
});
