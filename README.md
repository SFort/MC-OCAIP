## Offline Customization And Identification Protocol (OCAIP)

Allows connections to a server even when auth servers are unreachable.

The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.


____________________________________________________

### Download
Tested on Minecraft version: 1.18

[Download latest version](https://github.com/SFort/MC-OCAIP/releases)

____________________________________________________

### Features: 
- Offline logins can be restricted by a registration password  
by creating `ocaip/password` and putting the password inside

- Offline logins can be restricted by a registration proof of work  
by creating `ocaip/sha1pow` and putting the amount of zero bits inside.  
The recommended value is: `24`  
They can also require multiple proofs of work, for example 2 23 leadingZero ones by inserting `23*2`.


____________________________________________________

### How it works (in a nutshell):  
1. Client creates a private key (this is kindof like a password but more secure)
2. Client connects to server
3. If the client hasn't connected before the server will prompt a registration password/proof of work
4. The server will remember the public key and later logins will be seamless


____________________________________________________

### Q&A:
- Where's the private key stored?

  The private key is stored in `.minecraft/ocaip.key`  
keeping a backup of the key is recommemded as losing it means  
you won't be able to access that account and will have to contact an admin or create a new one.

- How to reset user's account if they lost their public key (server sided)?

  Server hosts public keys are located in `ocaip/server_keys`. If a user lost their original
public key you can reset the account by removing their name and the key underneath the name

- My skin is the default one, can I change that?

  With this mod you can. To have a skin, place your 64x64 pixel skin file into `.minecraft/ocaip_skin.png`.  
I recommend you to use `assets/minecraft/textures/entity/steve.png` as a reference.


____________________________________________________

**OCAIP**: because the acronym kindof sounds like "Oh, Cape" (due to the migrator cape).  

#### How it works in the real world:

https://user-images.githubusercontent.com/1879846/192084366-2a80a7f2-d78e-4e72-83c3-d556ccbbc02b.mp4

