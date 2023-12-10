ALTER TABLE orion.accounts DROP COLUMN mute;
ALTER TABLE orion.accounts DROP COLUMN nick;
ALTER TABLE orion.accounts DROP COLUMN webadmin;
ALTER TABLE orion.accounts DROP COLUMN vpoints;
ALTER TABLE orion.accounts DROP COLUMN points;
ALTER TABLE orion.accounts DROP COLUMN sitelogged;
ALTER TABLE orion.accounts DROP COLUMN ipCreated;
ALTER TABLE orion.accounts DROP COLUMN 2ndpassword;
ALTER TABLE orion.accounts DROP COLUMN salt2;


ALTER TABLE orion.`characters` DROP COLUMN `rank`;
ALTER TABLE orion.`characters` DROP COLUMN rankMove;
ALTER TABLE orion.`characters` DROP COLUMN jobRank;
ALTER TABLE orion.`characters` DROP COLUMN jobRankMove;


ALTER TABLE orion.`characters` DROP INDEX ranking2;
ALTER TABLE orion.`characters` DROP COLUMN gm;

ALTER TABLE orion.`characters` DROP COLUMN reborns;
ALTER TABLE orion.`characters` DROP COLUMN playerAutoReborn;
ALTER TABLE orion.`characters` DROP COLUMN playerAutoReborn;

ALTER TABLE orion.`characters` DROP COLUMN agentpoints;
ALTER TABLE orion.`characters` DROP COLUMN donatorpoints;

ALTER TABLE orion.`characters` DROP COLUMN gainedmsi;
ALTER TABLE orion.`characters` DROP COLUMN factionId;

ALTER TABLE orion.`characters` DROP COLUMN jumpxp;
ALTER TABLE orion.`characters` DROP COLUMN jumplevel;
ALTER TABLE orion.`characters` DROP COLUMN contributedFP;
ALTER TABLE orion.`characters` DROP COLUMN occupationId;
ALTER TABLE orion.`characters` DROP COLUMN occupationEXP;

DROP TABLE orion.ipvotelog;
DROP TABLE orion.gmlog;
DROP TABLE orion.macfilters;
