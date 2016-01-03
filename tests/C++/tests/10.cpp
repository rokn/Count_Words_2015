int main(){
	char grade = 'D';
	char grade_f;
	switch(grade){
		case 'A' :
			grade_f = 'A';
			break;
		case 'B' :
		case 'C' :
			grade_f = 'B';
			break;
		default :
			grade_f = 'I';
   }
	return 0;
}