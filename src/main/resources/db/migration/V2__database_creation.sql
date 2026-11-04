CREATE SEQUENCE user_challenge_sequence
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE TABLE users_challenge (
                                 id NUMBER(19,0) PRIMARY KEY,
                                 full_name VARCHAR2(255 CHAR),
                                 password VARCHAR2(255 CHAR),
                                 email VARCHAR2(255 CHAR),
                                 role VARCHAR2(20 CHAR),
                                 phone_number VARCHAR2(255 CHAR),

                                 CONSTRAINT uk_users_challenge_email UNIQUE (email)
);
