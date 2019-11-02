CREATE TABLE IF NOT EXISTS ` residence_functions `
(
    `
    id
    `
    TINYINT
    UNSIGNED
    NOT
    NULL
    DEFAULT
    '0',
    `
    type
    `
    TINYINT
    UNSIGNED
    NOT
    NULL
    DEFAULT
    '0',
    `
    lvl
    `
    SMALLINT
    UNSIGNED
    NOT
    NULL
    DEFAULT
    '0',
    `
    lease
    `
    INT
    NOT
    NULL
    DEFAULT
    '0',
    `
    rate
    `
    bigint
    NOT
    NULL
    DEFAULT
    '0',
    `
    endTime
    `
    INT
    NOT
    NULL
    DEFAULT
    '0',
    `
    inDebt
    `
    TINYINT
    NOT
    NULL
    DEFAULT
    '0',
    PRIMARY
    KEY
(
    `
    id
    `,
    `
    type
    `
)
    ) ENGINE=MyISAM;
