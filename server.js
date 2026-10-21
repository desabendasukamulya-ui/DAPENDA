const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const HTML_FILE = path.join(__dirname, 'dapenda_web_portal.html');

const server = http.createServer((req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const cleanUrl = req.url.split('?')[0];

  if (cleanUrl === '/' || cleanUrl === '/index.html' || cleanUrl === '/sidapenda' || cleanUrl === '/dapenda') {
    fs.readFile(HTML_FILE, (err, data) => {
      if (err) {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('Error loading SIDAPENDA Web Portal: ' + err.message);
        return;
      }
      res.writeHead(200, {
        'Content-Type': 'text/html; charset=utf-8',
        'Cache-Control': 'no-cache'
      });
      res.end(data);
    });
  } else {
    // Serve any other requested file if it exists, or fallback to HTML_FILE
    const filePath = path.join(__dirname, cleanUrl);
    if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
      fs.createReadStream(filePath).pipe(res);
    } else {
      fs.readFile(HTML_FILE, (err, data) => {
        if (!err) {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          res.end(data);
        } else {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('Not Found');
        }
      });
    }
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`SIDAPENDA Web Portal online server listening on port ${PORT}`);
});
