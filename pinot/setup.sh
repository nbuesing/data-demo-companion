#!/bin/bash

cd "$(dirname -- "$0")"

#
# The pino schema and table creation can be done through the UI, but
# having it in a script makes it easier to demonstrate and document.
# 

#
# aliases to make the scripts easier to maintain, bash does not expand aliases, by default, if invoked from a terminal; so 
# make sure that alias expansion is enabled for bash.
#
shopt -s expand_aliases
alias kt='kafka-topics --bootstrap-server localhost:9092'
alias pinot='./pinot.sh'
alias dc='docker compose'


(cd local; dc up -d --wait)

#
# schemas must align to how the data is represented on the kafka topic.
#
pinot schema email
pinot schema phone
pinot schema customer
pinot schema address
pinot schema ticket



#
# kafka topics must exist before the tables are created in pinot, since tables also define where the data is coming from
#
kt --create --if-not-exists --partitions 4 --topic data-demo-emails
kt --create --if-not-exists --partitions 4 --topic data-demo-phones
kt --create --if-not-exists --partitions 4 --topic data-demo-customers
kt --create --if-not-exists --partitions 4 --topic data-demo-addresses
kt --create --if-not-exists --partitions 4 --topic data-demo-tickets


pinot table email
pinot table phone
pinot table customer
pinot table address
pinot table ticket

