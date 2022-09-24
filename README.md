Intended to allow connection to a server even when auth servers are unreachable.  
The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.

Offline logins can be restricted by a registration password  
by creating `ocaip/password` and putting the password inside

Offline logins can be restricted by a registration proof of work  
by creating `ocaip/sha1pow` and putting the amout of zero bits inside  
recommended value: `24`  
they can also require multiple proofs of work, for example 2 23 leadingZero ones by inserting `23*2`

to have a skin place a 64x64 pixel skin file into `.minecraft/ocaip_skin.png`

OCAIP: Offline Customization And Identification Protocol because the acronym kindof sounds like "Oh, Cape" (due to the migrator cape).  



https://user-images.githubusercontent.com/1879846/192084366-2a80a7f2-d78e-4e72-83c3-d556ccbbc02b.mp4

