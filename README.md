# About

This SpringBoot Application reads an uploaded file sen via a REST POST request.

The uploaded file is expected to have the format :

```
UUID, ID, Name, Likes, Transport, Avg Speed, Top Speed
```

where each field is separated by a vertical bar character "|". 

It returns an "Outcome File", which is the result of processing the file.