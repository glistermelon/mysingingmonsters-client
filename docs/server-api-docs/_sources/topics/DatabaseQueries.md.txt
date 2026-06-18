# Database Queries

The API includes a variety of endpoints to query databases for information that the client is expected to cache (long-term).
The response always includes these parameters, which are both timestamps:

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - last_updated
  - long
* - server_time
  - long
```

These are typically accompanied by a list of relevant objects.
Only objects that are new or updated since the `last_updated` timestamp sent
*in the request* are included in the response.

```{warning}
If there are no updates or new objects, the "list of relevant objects" is simply not sent; that is, the field is not included in the response.
```

## Chunked Responses

If the response has to send a large amount of objects (I'm not exactly sure how many), it will be sent in chunks.
A chunked response is sent as a series of events with the above fields and these additional fields.
The client can use the presence of these fields as an indicator as to whether or not the response is chunked.

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - chunk
  - integer
  - Which chunk it is in the sequence, starting at 1 and ending at `numChunks` (inclusively).
* - numChunks
  - integer
  - How many chunks are in the sequence.
```