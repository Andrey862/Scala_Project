CREATE TABLE USERS (
  ID BIGSERIAL PRIMARY KEY,
  USERNAME VARCHAR NOT NULL UNIQUE,
  HASH VARCHAR NOT NULL
);

CREATE TABLE PRODUCT (
  ID BIGSERIAL PRIMARY KEY,
  TITLE VARCHAR NOT NULL,
  DESCRIPTION VARCHAR NOT NULL,
  POSTERID INT8 NOT NULL REFERENCES USERS (ID) ON DELETE CASCADE
);

CREATE TABLE REVIEW (
  ID BIGSERIAL PRIMARY KEY,
  DESCRIPTION VARCHAR NOT NULL,
  SCORE INT NOT NULL,
  PRODUCTID INT8 NOT NULL REFERENCES PRODUCT (ID) ON DELETE CASCADE,
  AUTHORID INT8 NOT NULL REFERENCES USERS (ID) ON DELETE CASCADE
);
