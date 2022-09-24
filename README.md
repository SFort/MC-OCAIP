# Minecraft Server Offline Customization And Identification Protocol (OCAIP)

Allows connections to a server even when auth servers are unreachable.

The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.

____________________________________________________

# Download
Tested on Minecraft version: 1.18

[Download latest version](https://github.com/SFort/MC-OCAIP/releases)
____________________________________________________

# Options: 
- Offline logins can be restricted by a registration password  
by creating `ocaip/password` and putting the password inside

- Offline logins can be restricted by a registration proof of work  
by creating `ocaip/sha1pow` and putting the amout of zero bits inside  
recommended value: `24`  
they can also require multiple proofs of work, for example 2 23 leadingZero ones by inserting `23*2`

- to have a skin place a 64x64 pixel skin file into `.minecraft/ocaip_skin.png`  
i recommend you use `assets/minecraft/textures/entity/steve.png` as a refrance

____________________________________________________

# How it works (in a nutshell):  
- 1. client creates a private key (this is kindof like a password but more secure)
- 2. client connects to server
- 3. if the client hasn't connected before the server will prompt a registration password/proof of work
- 4. the server will remember the public key and later logins will be seamless

____________________________________________________

# Q&A:
- Where's the private key stored?
the private key is stored in `.minecraft/ocaip.key`  
keeping a backup of the key is recommemded as losing it means  
you won't be able to access that account and will have to contact an admin or create a new one.

for server hosts public keys are located in `ocaip/server_keys` if a user has lost their
public key you can reset the account by removing their name and the key underneath the name
____________________________________________________

OCAIP: Offline Customization And Identification Protocol because the acronym kindof sounds like "Oh, Cape" (due to the migrator cape).  
____________________________________________________

# How it works in the real world:

https://user-images.githubusercontent.com/1879846/192084366-2a80a7f2-d78e-4e72-83c3-d556ccbbc02b.mp4

