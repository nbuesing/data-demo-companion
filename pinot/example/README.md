
schema.json

this creates a schema called `foo`, it is based on the data-demo-events topic.

table.json

this create a table called `foo`, with a schema also of named `foo`.

it is configured to pull from topic `data-demo-events`.

it has a start schema

it rolls segments when they get to 5000 rows (to show indexing).

```
select venueid, sum(capacity) from foo group by 1
```

```
explain plan for select venueid, sum(capacity) from foo group by 1
```

manually disabling STI
```
select venueid, sum(capacity) from foo group by 1
OPTION(useStarTree=false)
```

update data-demo/mockdata-daemon to produce more events, here is an example:

KafkaDaemon
```
  // every 20 seconds
  @Scheduled(cron = "*/5 * * * * *")
  public void createEvent() {
    IntStream.range(0, 1000).forEach(i -> {
      musicService.createEvent();
    });
  }
```
