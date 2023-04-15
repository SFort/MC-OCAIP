## Offline Customization And Identification Protocol (OCAIP)

Allows connections to a server even when auth servers are unreachable.

The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.


____________________________________________________

### Download

1.19.3 - [1.3.0](https://github.com/SFort/MC-OCAIP/releases/tag/r1.3.0)  
1.19.2 - [1.2.5](https://github.com/SFort/MC-OCAIP/releases/tag/r1.2.5)  
1.18.2 - [1.2.5-1.18.2](https://github.com/SFort/MC-OCAIP/releases/tag/r1.2.5)

____________________________________________________

### Features: 
- Offline logins can be restricted by a registration password  

- Offline logins can be restricted by a registration proof of work  

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

  To have a skin, place your 64x64 pixel skin file into `.minecraft/ocaip_skin.png`.  

____________________________________________________

**OCAIP**: because the acronym kindof sounds like "Oh, Cape" (due to the migrator cape).  

#### What it looks like:

https://user-images.githubusercontent.com/1879846/192084366-2a80a7f2-d78e-4e72-83c3-d556ccbbc02b.mp4

