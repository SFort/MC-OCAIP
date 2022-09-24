Intended to allow connection to a server even when auth servers are unreachable.  
The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.

Offline logins can be restricted by a registration password  
by creating `ocaip/password` and putting the password inside

Offline logins can be restricted by a registration proof of work  
by creating `ocaip/sha1pow` and putting the amout of zero bits inside  
recommended value: `24`  
they can also require multiple proofs of work, for example 2 23 leadingZero ones by inserting `23*2`

to have a skin place a 64x64 pixel skin file into `.minecraft/ocaip_skin.png`  
i recommend you use `assets/minecraft/textures/entity/steve.png` as a refrance

so in a nutshell how this works:  
- client creates a private key (this is kindof like a password but more secure)
- client connects to server
- if the client hasn't connected before the server will prompt a registration password/proof of work
- then the server will remember the public key and later logins will be seamless

the private key is stored in `.minecraft/ocaip.key`  
keeping a backup of the key is recommemded as loosing it means  
you won't be able to access that account and will have to contact an admin or create a new one.

for server hosts public keys are located in `ocaip/server_keys` if a user has lost their
public key you can reset the account by removing their name and the key underneath the name


OCAIP: Offline Customization And Identification Protocol because the acronym kindof sounds like "Oh, Cape" (due to the migrator cape).  



https://user-images.githubusercontent.com/1879846/192084366-2a80a7f2-d78e-4e72-83c3-d556ccbbc02b.mp4

