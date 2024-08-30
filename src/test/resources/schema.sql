CREATE TABLE IPREQUEST (
    requestid varchar(36) not null,
    requesturi varchar(255) not null,
    requesttimestamp timestamp not null,
    requestipaddress varchar(255) not null,
    requestcountrycode varchar(255) not null,
    requestipprovider varchar(255) not null
);