class TestingSwitch{

  public static void main(String[] args){
    int number = 8;
    
    switch(number){
      case (number >= 0 && number <= 9):
        System.out.println("one digit");
        break;
      case (number >= 10):
        System.out.println("more than one digit");
    }
  }
}
