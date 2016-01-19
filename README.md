# Tacoms
<h4>Prerequisites</h4>
  <ul>
    <li>Maven</li>
    <li>Java6</li>
    <li>OnePK SDK in local maven repo</li>
  </ul>
To add OnePK sdk to local maven repo follow: https://developer.cisco.com/media/onePKGettingStarted-v1-1-0/GUID-48637192-2337-45FA-B7AE-02263F686EE5.html


<h4>Download and Install</h4>
```
git clone https://github.com/idafroseth/tacoms/
```
Open eclipse and run 
```
file-import-existing maven project
```
Locate the file where the pom.xml in the newly added git folder.

<h4>Design</h4>
The NOR TACOMS contains multiple services which are identified in the TACOMS profile. For now it implements the SOW-6 profile. Each services run in its own thread and is managed by a Manager which also handles all updates of the GuI. All interaction with the router is done in the Model module where you will find a router-class and the VTY operations.  

GUI-Views>>Manager>>Services>>Router
