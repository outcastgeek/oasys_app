/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 12/24/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */

oasysUsaApp.factory('svgTextAnimService', [function () {
    return {
        animateText: function (element, attrs) {
            var text = attrs.text,
                textSize = parseFloat(attrs.textSize),
                bgFill = attrs.bgFill,
                circleFill = attrs.circleFill,
                s = Snap(element), circles = [],
                bg = s.rect(0, 0, 800, 200);

            bg.attr({
                'fill': bgFill || '#fff'
            });

            var circleGroup = s.group(bg);

            // create 200 circles
            for (var i = 0; i < 200; i++) {
                var size = Math.random() * 5 + 3,
                    cx = Math.random() * 800,
                    cy = Math.random() * 200,
                    opacity = Math.random(),
                    fill = circleFill || '#9d77da',
                    counter = Math.random() * 360;
                circ = s.circle(cx, cy, size);
                circ.attr({
                    'fill': fill,
                    'fill-opacity': opacity
                });
                circ.data('xOffset', cx);
                circ.data('cx', cx);
                circ.data('yOffset', cy);
                circ.data('cy', cy);
                circ.data('counter', counter);
                circles.push(circ);
                circleGroup.add(circ);

            }

            var increase = Math.PI * 2 / 40,
                text = s.text(10, 130, text);

            text.attr({
                'font-size': textSize,
                'fill': '#fff'
            });

            circleGroup.attr({
                mask: text
            });

            function draw() {
                for (var i = 0, l = circles.length; i < l; i++) {
                    var circ = circles[i];

                    if (circ.data('cy') < 0) {
                        circ.data('cy', 200);
                    } else {
                        circ.data('cy', (circ.data('cy') - 2));
                    }
                    circ.data('cx', (circ.data('xOffset') + (50 * (Math.sin(circ.data('counter')) / 5))));
                    circ.attr({
                        cx: circ.data('cx'),
                        cy: circ.data('cy')
                    });

                    circ.data('counter', circ.data('counter') + increase);
                }

            }

            function animate() {
                draw();
                window.requestAnimationFrame(animate);
            }

            animate();
        }
    }
}]);

oasysUsaApp.directive('svgTextAnim', function (svgTextAnimService) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            svgTextAnimService.animateText(element[0], attrs);
        }
    };
});

