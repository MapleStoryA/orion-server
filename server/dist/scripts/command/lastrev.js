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

importPackage(java.lang);
importPackage(java.io);

importPackage(Packages.client);
importPackage(Packages.client.messages);
importPackage(Packages.server);

function getDefinition () {
	var ret = java.lang.reflect.Array.newInstance(CommandDefinition, 1);
	ret[0] = new CommandDefinition("revinfo", "", "Gives information about the most recent revision.", "100"); 
	return ret;
}

function execute (c, mc, splitted) {
	var file = new File("lastrev.txt");
	
	var input =  new BufferedReader(new FileReader(file));
	try {
		var line = null;
		while (( line = input.readLine()) != null) {
			mc.dropMessage(line);
        	}
      	} finally {
		input.close();
	}
}