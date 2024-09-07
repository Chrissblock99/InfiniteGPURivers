This project aims to generate rivers on a procedurally generated terrain efficiently.
To achieve this the method desccribed in [this research paper](http://www.cescg.org/CESCG-2011/papers/TUBudapest-Jako-Balazs.pdf) is implemented and adapted to be 3x3 bounded.
Specifically the sediment transportation algorithm has been adapted to use pipes like water and thermal processes.
Credit also goes to [Stefan Gustavson AKA stegu](https://github.com/stegu) for the simplex noise implementation and [this opengl sample](http://forum.lwjgl.org/index.php?topic=6213.0) which i built all my code from.

# Compiling
It won't lol, theres some filepaths which are specific to my machine (and Linux) so you will have to change those.
I will happily accept PRs that fix this.
