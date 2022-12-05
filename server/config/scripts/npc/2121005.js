function start() {
status = -1;
action(1, 0, 0);
}
function action(mode, type, selection) {
if (mode == -1) {
cm.dispose();
}
else {
if (mode == 0) {
cm.dispose();
return;
}
if (mode == 1) {
status++;
}
else {
status--;
}
if (mode == 1 && status == 2 && selection == 0) {
cm.playSound(true, "orbis/do");
status--;
}
if (mode == 1 && status == 2 && selection == 1) {
cm.playSound(true, "orbis/la");
status--;
}
if (mode == 1 && status == 2 && selection == 2) {
cm.playSound(true, "orbis/mi");
status--;
}
if (mode == 1 && status == 2 && selection == 3) {
cm.playSound(true, "orbis/pa");
status--;
}
if (mode == 1 && status == 2 && selection == 4) {
cm.playSound(true, "orbis/re");
status--;
}
if (mode == 1 && status == 2 && selection == 5) {
cm.playSound(true, "orbis/si");
status--;
}
if (mode == 1 && status == 2 && selection == 6) {
cm.playSound(true, "orbis/sol");
status--;
}
if (status == 0) {
    cm.sendNextS("Let's rock.", 2);
       } if (status == 1) {
            cm.sendSimple("\r\n#L0##s12101002##l  #L1##s14101006##l  #L2##s13101003##l \r\n#L3##s10001015##l  #L4##s15111001##l  #L5##s0001011##l  \r\n                   #L6##s5201004##l");
        } if (status == 2) {
            }
        }
    }