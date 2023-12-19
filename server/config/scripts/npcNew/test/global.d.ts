/**
 * Interface for managing quest records.
 * Important Notes:
 * 1. The key is 4 bytes in size.
 * 2. The maximum length of the value is 16 characters (8 in Korean).
 */
export interface UQuestRecord {
    /**
     * Adds or updates a quest in the list with the given key and value.
     *
     * @param key   The key for the quest.
     * @param value The value associated with the quest.
     */
    set(key: number, value: string): void;

    /**
     * Marks a quest as complete.
     *
     * @param key The key of the quest to mark as complete.
     */
    setComplete(key: number): void;

    /**
     * Sets the state of a quest.
     * The set and setComplete methods relate to [ Key: quest ID ]
     * [ State: 1 -> Marks the quest as in progress / 2 -> Marks the quest as completed. ]
     * If the state is set to 0, it indicates that the quest is not started.
     *
     * @param key   The key of the quest.
     * @param state The state of the quest.
     */
    setState(key: number, state: number): void;

    /**
     * Returns the value associated with the given quest key.
     * Returns an empty string if the quest is not found.
     *
     * @param key The key of the quest.
     * @return The value associated with the quest.
     */
    get(key: number): string;

    /**
     * Gets the state of the quest.
     *
     * @param key The key of the quest.
     * @return The state of the quest.
     */
    getState(key: number): number;

    /**
     * Checks if a quest can be completed.
     * Return Values: -1 -> Quest not found or invalid / 0 -> Completion not allowed / 1 -> Completion allowed
     *
     * @param key The key of the quest.
     * @return The completion status.
     */
    canComplete(key: number): number;

    /**
     * Removes a quest entry with the given key.
     *
     * @param key The key of the quest to remove.
     */
    remove(key: number): void;

    /**
     * Records the mob selected for the quest.
     *
     * @param questId           The ID of the quest.
     * @param mobId             The ID of the mob.
     * @param locationType      The type of location (0 if not a text location, otherwise location value).
     * @param encounterLocation The encounter location.
     */
    selectedMob(questId: number, mobId: number, locationType: number, encounterLocation: number): void;
}

export interface BaseScripting {
    random(min: number, max: number): number;
}

export interface IFieldScripting {
    id(): number;

    getUserCount(): number;

    getMobCount(mobId: number): number; // MobID
    getMobHP(mobId: number): number; // MobID: Returns Mob's HP. Valid only when a single Mob with the specific ID exists. Returns -1 if Mob does not exist.
    countUserInArea(areaName: string): number; // AreaName
    countMaleInArea(areaName: string): number;

    countFemaleInArea(areaName: string): number;

    enablePortal(portalName: string, open: number): void; // PortalName, Open:1, Close:0
    effectObject(objName: string): void; // ObjName
    effectScreen(name: string): void; // Name
    effectSound(soundName: string): void;

    effectTremble(type: number, delay: number): void; // Type(0: Light&Long, 1: Heavy&Short), Delay(millisecond)
    notice(type: number, message: string, ...additionalParams: any[]): void; // Type(0: normal, 1: alert, 4: slide, 7: NPCSay), Message, additional parameters
    isItemInArea(areaName: string, itemId: number): number; // AreaName, ItemID
    summonMob(x: number, y: number, itemId: number): void; // x, y, ItemID
    transferFieldAll(mapCode: number, portalName: string): number; // MapCode, PortalName (Use is prohibited for moving inside->outside of FieldSet or outside->inside)
    setNpcVar(npcId: number, key: string, value: string): void; // NpcID, key, value (even integer values must be quoted, e.g., "1")
    getNpcStrVar(npcId: number, varName: string): string; // NpcID, VarName
    getNpcIntVar(npcId: number, varName: string): number; // NpcID, VarName
    setProtectMobDamagedByMob(setting: number): void; // Set or Reset
    removeAllMob(): void;

    setMobGen(setting: number): void; // On or Off
    removeMob(mobId: number): void;

    snowOn(setting: number): number; // Set or Reset
    nuffMob(arg1: number, arg2: number, arg3: number): void;

    isUserExist(userId: number): number; // UserID [Return]: Exist: 1, Not Exist: 0
    startEvent(): void; // Start Monster Carnival
    summonNpc(templateId: number, x: number, y: number): void; // templateId, x, y: Summon NPC
    vanishNpc(templateId: number): void; // templateId: Remove NPC
    getGameEventManager(): any;
}


export interface ITargetScripting {
    getCharacterID(): number;

    getCharacterName(): string;

    getGender(): number;

    getHair(): number;

    getFace(): number;

    nLevel(): number;

    nJob(): number;

    changeJob(job: number): boolean;

    setJob(job: number): boolean;

    nSTR(): number;

    incSTR(value: number): number;

    nDEX(): number;

    incDEX(value: number): number;

    nINT(): number;

    incINT(value: number): number;

    nLUK(): number;

    incLUK(value: number): number; // TypeScript does not have a 'short' type, using number
    nHP(): number;

    incHP(value: number): number;

    nMP(): number;

    incMP(value: number): number;

    incMHP(value: number, other: number): number;

    incMMP(value: number, other: number): number;

    nAP(): number;

    incAP(value: number): number;

    incAP(value: number, a: number): number;

    nSP(): number;

    incSP(value: number): number;

    incSP(value: number, a: number): number;

    isMaster(): boolean;

    isSuperGM(): boolean;

    message(text: string): void;

    incEXP(total: number, show: boolean): void;

    incEXP(total: number, show: number): void;

    isPartyBoss(): boolean;

    isOnParty(): boolean;

    getPartyMembersCount(): number;

    transferParty(map: number, portal: string, option: number): number;

    playPortalSE(): void;

    registerTransferField(map: number, portal: string): void;

    field(): IFieldScripting;

    fieldID(): number;

    nMoney(): number;

    incMoney(meso: number, show: number): number;

    incMoney(meso: number, show: boolean): number;

    decMoney(meso: number, show: boolean): number;

    set(key: string, value: string): void;

    get(key: string): string;

    setVar(key: string, value: any): void; // TypeScript uses 'any' for a generic object type
    getVar(key: string): any;

    clearTemporaryData(): void;

    getEventManager(): any;

    isEvan(): boolean;

    isDualBlade(): boolean;

    isNightWalker(): boolean;

    isAnyKindOfThief(): boolean;

    isAran(): boolean;

    haveItem(id: number): boolean;

    getEvent(): any;

    getWarpMap(map: number): any;

    getMap(map: number): any;

    getQuestStatus(id: number): number;

    isQuestActive(id: number): boolean;

    isQuestFinished(id: number): boolean;

    completeQuest(id: number, npcId: number): void;

    forfeitQuest(id: number): void;

    forceCompleteQuest(id: number, npcId: number): void;

    changeMusic(music: string): void;

    questRecord(): UQuestRecord; // Assuming UQuestRecord is a type you have defined elsewhere
    inventory(): any; // Assuming InventoryScripting is a type you have defined elsewhere
    field(): IFieldScripting;
}

interface IInventoryScripting {
    slotCount(type: number): number;

    holdCount(type: number): number;

    itemCount(item: number): number;

    exchange(money: number, id: number, quantity: number): number;

    incSlotCount(type: number, value: number): void;

    // Overloaded method for 'exchange' with variable number of arguments
    exchange(money: number, ...items: number[]): number;
}

interface INpcScripting {
    field(): IFieldScripting;

    say(text: string): void;

    sayUser(text: string): void;

    sayOk(text: string): void;

    sayOkUser(text: string): void;

    askYesNo(text: string): number;

    askYesUser(text: string): number;

    askAccept(text: string): number;

    askAcceptUser(text: string): number;

    askAvatar(text: string, item: number, ...styles: number[]): number;

    makeRandAvatar(item: number, ...styles: number[]): number;

    askMenu(text: string): number;

    askMenuUser(text: string): number;

    askText(text: string, def: string, col: number, line: number): number;

    askTextUser(text: string, def: string, col: number, line: number): number;

    askNumber(text: string, def: number, min: number, max: number): number;

    askNumberUser(text: string, def: number, min: number, max: number): number;

    setSpecialAction(npcId: number, action: string): void;
}


declare global {
    let target: ITargetScripting;
    let inventory: IInventoryScripting;

    interface Window extends INpcScripting, BaseScripting {

    }
}
export {};