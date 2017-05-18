#!/bin/bash

mysqldump -u hsuser -phspass hs | gzip > $1
