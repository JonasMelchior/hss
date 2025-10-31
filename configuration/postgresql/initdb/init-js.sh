#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    create role hss_user with login password 'mighty_hss_pwd';
    create database hss with owner hss_user;
EOSQL

