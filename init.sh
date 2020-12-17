read -p "Package (eg. com.niton)                :" package
read -p "Repository: (eg. nbrugger-tgm/react4j) :" repo
read -p "Name: (eg. react4j)                    :" name
sed -i "s/{{package}}/$package/" *
sed -i "s/{{repo}}/$repo/" *
sed -i "s/{{name}}/$name/" *

