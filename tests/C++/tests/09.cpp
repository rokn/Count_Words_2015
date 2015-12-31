int main ()
{
   char grade = 'D';

   switch(grade)
   {
   case 'A' :
      cout << "Excellent!" << endl; 
      break;
   case 'B' :
   case 'C' :
      cout << "Well done" << endl;
      break;
   default :
      cout << "Invalid grade" << endl;
   }
 
   return 0;
}