# Chord
This repository contains the implementation in Java of the Chord protocol to build up a distributed and fault tolerant hash table, and it's part of the Distributed Systems course at the Computer Science master degree at Politecnico of Milan, academic year 19/20. A related project can be found at https://github.com/arancicarini/Middleware-2019-2020. For more info about the protocol, refer to the paper.

The project comes as a Java library, with the following static methods of the Chord class which are the APIs of the library:
methods | params | meaning | throws | return |
| create |String IPAddress, int port | create a new Chord and add a new node at `IPAddress` and `port` | PortException if `port` is not available | void |
| join   | String IPAddress, int port, String knownIPAddress, int knownPort | join an existing Chord through the node at `knownIPAddress` and `knownPort` and create a node at `IPAddress` and `port`| PortException if `port` is not available,  NotInitializedException if the known node does not reply | void |
| publish | Object o, int port |   Insert an object o into the distributed hashtable using the local node at `port` | NotInitializedException  if there is no node with such port number | a String: the key of the object |
| lookup | String key, int port | lookup of an object through the node at `port` | NotInitializedException if there is no node with such port number | the file associated with `key` or null if the file does not exist in the hash table | 
| deleteFile | String key, int port | Delete the file associated with `key` from the distributed filesystem | NotInitializedException if there is no node with such port number | void |
| deleteNode | int port | Delete the node associated with `port` from the Chord it's part of | NotInitializedException if there is no node with such port number | void |
| deleteAll | - | Delete all the local node from the Chords they are part of | - | void |

Some rough analysis about performances have been performed too.
The implementation allows to specify in the code ( in the Utilities class) the number of bytes to take from the SHA-1 hash ( which is of 160 bits) of the string IPAddress + port to identify nodes. Currently the library uses 16 bits. Using a small number of bits speeds up the code but increases the risk of collisions.
