/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 11/29/13
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */

var zmq = require('zmq')
    , sock = zmq.socket('pull');

sock.connect('tcp://127.0.0.1:3000');
console.log('Worker connected to port 3000');

sock.on('message', function(msg){
    console.log('work: %s', msg.toString());
});


