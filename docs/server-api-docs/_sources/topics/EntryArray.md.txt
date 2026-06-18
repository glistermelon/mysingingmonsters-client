# Entry Arrays

Some data is sent as what I call an "entry array," where instead of sending something like this:
```
{
    key1: value1,
    key2: value2,
    key3: value3
}
```

It's instead sent like this. Notice the square brackets indicating this is an array.
```
[
    { key1: value1 },
    { key2: value2 },
    { key3: value3 }
]
```