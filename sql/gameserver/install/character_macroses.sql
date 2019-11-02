CREATE TABLE IF NOT EXISTS ` character_macroses `
(
    `
    char_obj_id
    `
    INT
    NOT
    NULL
    DEFAULT
    '0',
    `
    id
    `
    SMALLINT
    UNSIGNED
    NOT
    NULL
    DEFAULT
    '0',
    `
    icon
    `
    TINYINT
    UNSIGNED,
    `
    name
    `
    VARCHAR
(
    40
) CHARACTER SET UTF8 DEFAULT NULL,
    ` descr ` VARCHAR
(
    80
) CHARACTER SET UTF8 DEFAULT NULL,
    ` acronym ` VARCHAR
(
    4
) CHARACTER SET UTF8 DEFAULT NULL,
    ` commands ` VARCHAR
(
    1024
) CHARACTER SET UTF8 DEFAULT NULL,
    PRIMARY KEY
(
    `
    char_obj_id
    `,
    `
    id
    `
)
    ) ENGINE=MyISAM;
