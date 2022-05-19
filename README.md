Intended to allow connection to a server even when auth servers are unreachable.
The goal is to just to add a bit of auth to offline-mode, while allowing normal online connections.
Will also sync skins at some point.

Offline logins can also be restricted by a registration password
by creating `ocaip.pass` and putting the password inside

Todo:

- [x] Id verification login
- [x] Require one time pass to bypass
- [ ] Local Skins / Capes