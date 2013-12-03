/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 11/29/13
 * Time: 12:53 PM
 * To change this template use File | Settings | File Templates.
 */

var zmq = require('zmq')
    , sock = zmq.socket('push');

sock.bindSync('tcp://127.0.0.1:3000');
console.log('Producer bound to port 3000');

setInterval(function(){
    console.log('sending work');
    sock.send('some work');
}, 500);

