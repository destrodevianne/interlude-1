CREATE TABLE IF NOT EXISTS ` second_auth `
(
    `login` VARCHAR
(
    45
) CHARACTER SET UTF8 NOT NULL DEFAULT '',
    ` password ` VARCHAR
(
    10
) NOT NULL DEFAULT '',
    ` tryLine ` TINYINT DEFAULT NULL,
    ` blockEndTime ` INT UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY
(
    `login`
)
    );
