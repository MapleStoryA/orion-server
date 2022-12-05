let questMap = 677000008;
let map = target.getMap(questMap);

let text = "#r[Requirements to Enter]#k\n\n\r\t\t\t#b1.You must be a Thief, Night Walker, or Dual Blade.#k";
text += "\n\r\t\t\t#b2. You must be under Level 40.#k";
text += "\n\r\t\t\t#b3. You must have the Large Model of a Coin.#k";

self.say(text);

if(map.fieldMembersCount() > 0){
    self.say("Someone is already inside.");
    return;
}

if(target.isAnyKindOfThief() && target.nLevel() < 40 && inventory.itemCount(4032485) >= 1){
    self.say("You are permitted to enter the Demon's Doorway.");
    inventory.exchange(0, 4032485, -1);
    target.registerTransferField(questMap, "out00");
}else{
    self.say("You are not allowed to enter.");
}





