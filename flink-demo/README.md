0. Install Needed Software Components

   * Install the DataGen Connector, this connector is used to generate fake data.

     * The `./01-install-connector.sh` script does this for you, it will pull the connector from the internet using curl,
   and then unzipping it. This is done in the `kafka-1/connect-plugins` directory.

     * Verify by inspecting the directory.

      ```
      → ls -al ../kafka-1/connect-plugins
      total 15288
      drwxr-xr-x  5 buesing  staff      160 Mar  3 08:31 .
      drwxr-xr-x  6 buesing  staff      192 Mar  2 07:51 ..
      -rw-r--r--  1 buesing  staff        0 Mar  1 19:22 .gitkeep
      drwxr-xr-x  7 buesing  staff      224 Feb 22 13:18 confluentinc-kafka-connect-datagen-0.6.4
      -rw-r--r--  1 buesing  staff  7823793 Mar  3 08:31 confluentinc-kafka-connect-datagen-0.6.4.zip
      ```

   * Install `jq`

      * The command `jq` may already be installed; check to see if you have `jq` installed, but by just running `jq` on the command line

      ```
      → jq
      jq - commandline JSON processor [version 1.7]
   
      Usage:	jq [options] <jq filter> [file...]
      ...
      ```

      * If it is not installed, use various installers to install it (e.g. brew on MacOS). Or if you want to install it only for this this project, you can
      run `02-install-jq.sh` which will place it in the demo's bin directory (that scripts will put on their classpath).

      ```
      → ls -al bin
      total 1664
      drwxr-xr-x   4 buesing  staff     128 Mar  4 07:31 .
      drwxr-xr-x  17 buesing  staff     544 Mar  4 07:37 ..
      -rw-r--r--   1 buesing  staff       0 Mar  4 07:31 .gitkeep
      -rwxr-xr-x@  1 buesing  staff  807984 Mar  3 09:55 jq
      ```

1. 03-up.sh -- to start kafka-1 and flink 

2. 04-setup.sh 

   a. copy the datagen avro file into the connector data directory

   b. create the two kafka topics 
  
      * datagen.orders - created by the datagen plugin, the input for the flink application
      * purchase-orders - the output of the flink application

   c. Create the Datagen plugin so demo orders are created into the `datagen.orders` topic.

   d. build the Flink application

      * creates as a shadowJar (not my first choice, but easiest for demos)

      * copy jar to directory accessible by the job-manager containers

   e. start the flink application
 
      * set parallelization to 4 -- the same number as the partitions on the topic
   
3. Goto http://localhost:48081/ and check out Flink

4. consume `datagen.orders`

   * inspect topic to verify there is no pricing
 
   * `./consumer.sh datagen.orders`
   
5. consume `purchase-orders`

   * inspect topic to verify pricing has been done

   * `./consumer.sh purchase-orders`
