Intended to allow connection to a server even when auth servers are unreachable.  
The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.

Offline logins can be restricted by a registration password
by creating `ocaip/password` and putting the password inside

Offline logins can be restricted by a registration proof of work
by creating `ocaip/sha1pow` and putting the about of zero bits inside
recommended value: `24`
they can also require multiple proofs of work, for example 2 23 leadingZero ones by inserting `23*2`



Originally called Offline Customization And Identification Protocol because the acronym kindof sounded like "Oh, Cape" (due to the migrator cape).  
It's not really valid anymore since i didn't add cape customization, still keeping the acronym.
