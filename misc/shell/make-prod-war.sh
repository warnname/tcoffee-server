../play war ../tcoffee -o ~/distrib/tcoffee --%prod
cd ~/distrib/tcoffee/
rm -rf WEB-INF/application/data/
rm -rf WEB-INF/application/macosx/
rm -rf WEB-INF/application/misc/  
rm -rf WEB-INF/application/templates/
rm -rf WEB-INF/application/test-result/
rm -rf WEB-INF/application/tmp/        
rm -rf WEB-INF/application/eclipse/
mv WEB-INF/application/META-INF .
find . -name .svn | xargs rm -rf
find . -name .settings | xargs rm -rf
find . -name .project | xargs rm -rf
zip -r ../tcoffee-prod.war *

 

