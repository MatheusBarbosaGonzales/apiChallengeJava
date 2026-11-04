CREATE TABLE documents_challenge (
                                     id NUMBER(19) PRIMARY KEY,
                                     title VARCHAR2(255) NOT NULL,
                                     file_name VARCHAR2(255) NOT NULL,
                                     status VARCHAR2(30) NOT NULL,
                                     signer_id NUMBER(19),
                                     created_at TIMESTAMP,
                                     signed_at TIMESTAMP,

                                     CONSTRAINT fk_document_signer
                                         FOREIGN KEY (signer_id)
                                             REFERENCES users_challenge(id)
);

CREATE SEQUENCE document_challenge_sequence
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;