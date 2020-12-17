read -p "Package (eg. com.niton)                :" package
read -p "Repository: (eg. nbrugger-tgm/react4j) :" repo
read -p "Name: (eg. react4j)                    :" name
sed -i "s|{{package}}|$package|" build.gradle settings.gradle README-TEMPLATE.md 
sed -i "s|{{repo}}|$repo|" build.gradle settings.gradle README-TEMPLATE.md 
sed -i "s|{{name}}|$name|" build.gradle settings.gradle README-TEMPLATE.md 
rm README.md
mv README-TEMPLATE.md README.md

