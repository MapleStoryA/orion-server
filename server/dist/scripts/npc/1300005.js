
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    //cm.sendOk("We must protect the kingdom!");
    cm.forceStartQuest(2314); 
    
    cm.dispose();
}