DROP TABLE IF EXISTS ` class_list `;
CREATE TABLE ` class_list `
(
    `class_name` varchar
(
    19
) NOT NULL default '',
    ` id ` int
(
    10
) UNSIGNED NOT NULL default '0',
    ` parent_id ` int
(
    11
) NOT NULL default '0',
    ` parent_id2 ` int
(
    11
) NOT NULL default '0',
    PRIMARY KEY
(
    `id`
)
    ) ENGINE=MyISAM;

insert into class_list
values ('H_Fighter', 0, -1, 0),
       ('H_Warrior', 1, 0, 0),
       ('H_Gladiator', 2, 1, 0),
       ('H_Duelist', 88, 2, 0),
       ('H_Warlord', 3, 1, 0),
       ('H_Dreadnought', 89, 3, 0),
       ('H_Knight', 4, 0, 0),
       ('H_Paladin', 5, 4, 0),
       ('H_PhoenixKnight', 90, 5, 0),
       ('H_DarkAvenger', 6, 4, 0),
       ('H_HellKnight', 91, 6, 0),
       ('H_Rogue', 7, 0, 0),
       ('H_TreasureHunter', 8, 7, 0),
       ('H_Adventurer', 93, 8, 0),
       ('H_Hawkeye', 9, 7, 0),
       ('H_Sagittarius', 92, 9, 0),
       ('H_Mage', 10, -1, 0),
       ('H_Wizard', 11, 10, 0),
       ('H_Sorceror', 12, 11, 0),
       ('H_Archmage', 94, 12, 0),
       ('H_Necromancer', 13, 11, 0),
       ('H_Soultaker', 95, 13, 0),
       ('H_Warlock', 14, 11, 0),
       ('H_ArcanaLord', 96, 14, 0),
       ('H_Cleric', 15, 10, 0),
       ('H_Bishop', 16, 15, 0),
       ('H_Cardinal', 97, 16, 0),
       ('H_Prophet', 17, 15, 0),
       ('H_Hierophant', 98, 17, 0),
       ('E_Fighter', 18, -1, 0),
       ('E_Knight', 19, 18, 0),
       ('E_TempleKnight', 20, 19, 0),
       ('E_EvaTemplar', 99, 20, 0),
       ('E_SwordSinger', 21, 19, 0),
       ('E_SwordMuse', 100, 21, 0),
       ('E_Scout', 22, 18, 0),
       ('E_PlainsWalker', 23, 22, 0),
       ('E_WindRider', 101, 23, 0),
       ('E_SilverRanger', 24, 22, 0),
       ('E_MoonlightSentinel', 102, 24, 0),
       ('E_Mage', 25, -1, 0),
       ('E_Wizard', 26, 25, 0),
       ('E_SpellSinger', 27, 26, 0),
       ('E_MysticMuse', 103, 27, 0),
       ('E_ElementalSummoner', 28, 26, 0),
       ('E_ElementalMaster', 104, 28, 0),
       ('E_Oracle', 29, 25, 0),
       ('E_Elder', 30, 29, 0),
       ('E_EvaSaint', 105, 30, 0),
       ('DE_Fighter', 31, -1, 0),
       ('DE_PaulusKnight', 32, 31, 0),
       ('DE_ShillienKnight', 33, 32, 0),
       ('DE_ShillienTemplar', 106, 33, 0),
       ('DE_BladeDancer', 34, 32, 0),
       ('DE_SpectralDancer', 107, 34, 0),
       ('DE_Assassin', 35, 31, 0),
       ('DE_AbyssWalker', 36, 35, 0),
       ('DE_GhostHunter', 108, 36, 0),
       ('DE_PhantomRanger', 37, 35, 0),
       ('DE_GhostSentinel', 109, 37, 0),
       ('DE_Mage', 38, -1, 0),
       ('DE_DarkWizard', 39, 38, 0),
       ('DE_Spellhowler', 40, 39, 0),
       ('DE_StormScreamer', 110, 40, 0),
       ('DE_PhantomSummoner', 41, 39, 0),
       ('DE_SpectralMaster', 111, 41, 0),
       ('DE_ShillienOracle', 42, 38, 0),
       ('DE_ShillienElder', 43, 42, 0),
       ('DE_ShillienSaint', 112, 43, 0),
       ('O_Fighter', 44, -1, 0),
       ('O_Raider', 45, 44, 0),
       ('O_Destroyer', 46, 45, 0),
       ('O_Titan', 113, 46, 0),
       ('O_Monk', 47, 44, 0),
       ('O_Tyrant', 48, 47, 0),
       ('O_GrandKhauatari', 114, 48, 0),
       ('O_Mage', 49, -1, 0),
       ('O_Shaman', 50, 49, 0),
       ('O_Overlord', 51, 50, 0),
       ('O_Dominator', 115, 51, 0),
       ('O_Warcryer', 52, 50, 0),
       ('O_Doomcryer', 116, 52, 0),
       ('D_Fighter', 53, -1, 0),
       ('D_Scavenger', 54, 53, 0),
       ('D_BountyHunter', 55, 54, 0),
       ('D_FortuneSeeker', 117, 55, 0),
       ('D_Artisan', 56, 53, 0),
       ('D_Warsmith', 57, 56, 0),
       ('D_Maestro', 118, 57, 0);
