# Brainfuck interpreter


To use:
```Java
Runnable myBf = new Brainfuck(InputStreamReader, OutputStreamWriter, "program string here");
myBf.run();
```

__Brainfuck implements `Runnable`__, therefore you could use it with a thread:
```Java
new Thread(myBf).start();
```
