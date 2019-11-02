DELETE
FROM character_friends;
DELETE
FROM character_hennas;
DELETE
FROM character_macroses;
DELETE
FROM character_quests;
DELETE
FROM character_recipebook;
DELETE
FROM character_shortcuts;
DELETE
FROM character_skills;
DELETE
FROM character_skills_save;
DELETE
FROM character_subclasses;
DELETE
FROM character_variables;
DELETE
FROM ex_achievements;
DELETE
FROM second_auth;
DELETE
FROM pets;
DELETE
FROM siege_clans;
DELETE
FROM items
WHERE item_id > 0;
DELETE
FROM characters;
DELETE
FROM clan_data;
DELETE
FROM heroes;
DELETE
FROM seven_signs;
DELETE
FROM ally_data;
DELETE
FROM bans;
DELETE
FROM bbs_clannotice;
DELETE
FROM bbs_memo;
DELETE
FROM castle_manor_production;
DELETE
FROM character_blocklist;
DELETE
FROM character_effects_save;
DELETE
FROM character_friends;
DELETE
FROM character_group_reuse;
DELETE
FROM character_mail;
DELETE
FROM character_post_friends;
DELETE
FROM character_premium_items;
DELETE
FROM character_recommends;
DELETE
FROM clan_privs;
DELETE
FROM clan_skills;
DELETE
FROM clan_subpledges;
DELETE
FROM clan_subpledges_skills;
DELETE
FROM clan_wars;
DELETE
FROM couples;
DELETE
FROM cursed_weapons;
UPDATE epic_boss_spawn
SET respawnDate=0,
    state=0;
DELETE
FROM event_data;
DELETE
FROM fishing_championship;
DELETE
FROM heroes_diary;
DELETE
FROM items_attributes;
DELETE
FROM items_delayed;
DELETE
FROM items_duration;
DELETE
FROM items_options;
DELETE
FROM items_period;
DELETE
FROM items_variation;
DELETE
FROM oly_comps;
DELETE
FROM oly_heroes;
DELETE
FROM oly_nobles;
DELETE
FROM oly_season;
DELETE
FROM pawnshop;
DELETE
FROM petitions;
DELETE
FROM raidboss_points;
DELETE
FROM raidboss_status;
DELETE
FROM seven_signs_festival;
INSERT INTO ` seven_signs_festival `
VALUES (0, "dawn", 1, 0, 0, "", ""), (1, "dawn", 1, 0, 0, "", ""), (2, "dawn", 1, 0, 0, "", ""), (3, "dawn", 1, 0, 0, "", ""), (4, "dawn", 1, 0, 0, "", ""), (0, "dusk", 1, 0, 0, "", ""), (1, "dusk", 1, 0, 0, "", ""), (2, "dusk", 1, 0, 0, "", ""), (3, "dusk", 1, 0, 0, "", ""), (4, "dusk", 1, 0, 0, "", "");
DELETE
FROM seven_signs_status;
INSERT INTO ` seven_signs_status `
VALUES (1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
DELETE
FROM castle;
INSERT INTO castle
VALUES ('1', 'Gludio', '0', '0', '0', '0', '0', '0'),
       ('2', 'Dion', '0', '0', '0', '0', '0', '0'),
       ('3', 'Giran', '0', '0', '0', '0', '0', '0'),
       ('4', 'Oren', '0', '0', '0', '0', '0', '0'),
       ('5', 'Aden', '0', '0', '0', '0', '0', '0'),
       ('6', 'Innadril', '0', '0', '0', '0', '0', '0'),
       ('7', 'Goddard', '0', '0', '0', '0', '0', '0'),
       ('8', 'Rune', '0', '0', '0', '0', '0', '0'),
       ('9', 'Schuttgart', '0', '0', '0', '0', '0', '0');
DELETE
FROM clanhall;
INSERT INTO clanhall
VALUES ('21', 'Fortress of Resistance', '0', '0', '0', '0', '0', null, '0', '0'),
       ('22', 'Moonstone Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('23', 'Onyx Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('24', 'Topaz Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('25', 'Ruby Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('26', 'Crystal Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('27', 'Onyx Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('28', 'Sapphire Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('29', 'Moonstone Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('30', 'Emerald Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('31', 'The Atramental Barracks', '0', '0', '0', '0', '0', null, '0', '0'),
       ('32', 'The Scarlet Barracks', '0', '0', '0', '0', '0', null, '0', '0'),
       ('33', 'The Viridian Barracks', '0', '0', '0', '0', '0', null, '0', '0'),
       ('34', 'Devastated Castle', '0', '0', '0', '0', '0', null, '0', '0'),
       ('35', 'Bandit Stronghold', '0', '0', '0', '0', '0', null, '0', '0'),
       ('36', 'The Golden Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('37', 'The Silver Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('38', 'The Mithril Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('39', 'Silver Manor', '0', '0', '0', '0', '0', null, '0', '0'),
       ('40', 'Gold Manor', '0', '0', '0', '0', '0', null, '0', '0'),
       ('41', 'The Bronze Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('42', 'The Golden Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('43', 'The Silver Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('44', 'The Mithril Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('45', 'The Bronze Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('46', 'Silver Manor', '0', '0', '0', '0', '0', null, '0', '0'),
       ('47', 'Moonstone Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('48', 'Onyx Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('49', 'Emerald Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('50', 'Sapphire Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('51', 'Mont Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('52', 'Astaire Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('53', 'Aria Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('54', 'Yiana Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('55', 'Roien Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('56', 'Luna Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('57', 'Traban Chamber', '0', '0', '0', '0', '0', null, '0', '0'),
       ('58', 'Eisen Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('59', 'Heavy Metal Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('60', 'Molten Ore Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('61', 'Titan Hall', '0', '0', '0', '0', '0', null, '0', '0'),
       ('62', 'Rainbow Springs', '0', '0', '0', '0', '0', null, '0', '0'),
       ('63', 'Wild Beast Reserve', '0', '0', '0', '0', '0', null, '0', '0'),
       ('64', 'Fortress of the Dead', '0', '0', '0', '0', '0', null, '0', '0');
DELETE
FROM siege_clans;
DELETE
FROM siege_players;
DELETE
FROM ` server_variables `;
INSERT IGNORE INTO ` server_variables ` (` name `, ` value `)
VALUES ('oly_season_id', '1');
