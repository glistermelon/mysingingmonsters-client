# Generic Update Properties

Many responses include the following field:

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - properties
  - [Generic Update](/data/GenericUpdate)
```

As the name implies, it includes generic update information.

It is so commonly sent that I haven't kept track of which endpoints actually send it.
Even if a certain response includes generic update properties
at one time, that does not mean it always includes them, if I recall correctly.
My approach is to tack generic update properties as an optional field on all responses.