public class Brick {
     sliderangel.setOnMouseDragged(e->{
        alignment.setVisible(true);
        Rotate rotateAngle=new Rotate();
        rotateAngle.setPivotX(cannonX);
        rotateAngle.setPivotY(cannonY);
        if(sliderangel.getValue()>temp){
            rotateAngle.setAngle(sliderangel.getValue()-temp);
            temp=sliderangel.getValue();
        }
        if (sliderangel.getValue()<temp) {
            rotateAngle.setAngle(-(temp-sliderangel.getValue()));
            temp=sliderangel.getValue();
        }

        alignment.getTransforms().add(rotateAngle);
    });
    sliderangel.setOnMouseReleased(e->{
        alignment.setVisible(false);
        Rotate rotateAngle=new Rotate();
        rotateAngle.setPivotX(cannonX);
        rotateAngle.setPivotY(cannonY);
        if(90>temp){
            rotateAngle.setAngle(90-temp);
            temp=sliderangel.getValue();

        }
        if (sliderangel.getValue()<temp) {
            rotateAngle.setAngle(-(temp-90));
            temp=sliderangel.getValue();
        }

        alignment.getTransforms().add(rotateAngle);
    });

    root.getChildren().add(alignment);
}

}
