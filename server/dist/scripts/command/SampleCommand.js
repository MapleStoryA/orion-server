/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

importPackage(Packages.client);
importPackage(Packages.client.messages);
importPackage(Packages.server);

function getDefinition () {
	var ret = java.lang.reflect.Array.newInstance(CommandDefinition, 1);
	ret[0] = new CommandDefinition("gmshop", "shopname", "Opens a GM shop. Just type !gmshop for a more thorough explanation.", "100"); 
	return ret;
}

function execute (c, mc, splitted) {
	if (splitted.length != 2) {
		mc.dropMessage("Syntax: !shop <shopid>, where shopid can be numerical or the following shops:");
		mc.dropMessage("mapleitems, glimmer, miscitems, scrolling, summonbags, warrior, magician, thief, bowman, nxrings, nxeffects, nxpackages, nxemotes, itembuffs, smega, gms, pets");
	} else if (splitted[1] == "mapleitems") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9500000);
		shop.sendShop(c);
	} else if (splitted[1] == "glimmer") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9510000);
		shop.sendShop(c);
	} else if (splitted[1] == "miscitems") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9520000);
		shop.sendShop(c);
	} else if (splitted[1] == "scrolling") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9530000);
		shop.sendShop(c);
	} else if (splitted[1] == "summonbags") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9540000);
		shop.sendShop(c);
	} else if (splitted[1] == "warrior") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9550000);
		shop.sendShop(c);
	} else if (splitted[1] == "magician") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9560000);
		shop.sendShop(c);
	} else if (splitted[1] == "thief") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9570000);
		shop.sendShop(c);
	} else if (splitted[1] == "bowman") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9580000);
		shop.sendShop(c);
	} else if (splitted[1] == "nxrings") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9590000);
		shop.sendShop(c);
	} else if (splitted[1] == "nxeffects") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9591000);
		shop.sendShop(c);
	} else if (splitted[1] == "nxpackages") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9592000);
		shop.sendShop(c);
	} else if (splitted[1] == "nxemotes") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9593000);
		shop.sendShop(c);
	} else if (splitted[1] == "itembuffs") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9594000);
		shop.sendShop(c);
	} else if (splitted[1] == "smega") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(9595000);
		shop.sendShop(c);                       
	} else if (splitted[1] == "gms") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(1337);
		shop.sendShop(c);       
	} else if (splitted[1] == "pets") {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(1000);
		shop.sendShop(c);       
	}else {
		var sfact = MapleShopFactory.getInstance();
		var shop = sfact.getShop(1337);
		shop.sendShop(c);
	}
}