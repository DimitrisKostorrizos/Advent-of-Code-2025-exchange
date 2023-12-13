use strict;

my $totalscore = 0;

my $time;
my $dist;


while (<>) {
	chop();
    if ( $_ =~ /Time: +(\d[ \d]+\d)/ ) {
        $time = ($1 =~ s/ +//rg) ;
    } elsif ( $_ =~ /Distance: +(\d[ \d]+\d)/ ) {
        $dist = ($1 =~ s/ +//rg);
    } 
}

my $low = 1;
my $high = $time-1;

# max distance should be at time/2 (according to math)
my $mid = $high;
while ($low<$mid) {
    my $speed = $low + int(($mid-$low)/2);
    my $d = $speed * ($time -$speed);
print "$speed: $d\n";
    if ( $dist < $d ) {
        $mid = $speed;
    } else {
        if ($low != $speed) {
            $low = $speed;
        } else {
            $mid = $low;
        }
    }
}
my $mid = $low;
while ($mid<$high) {
    my $speed = $mid + int(($high-$mid)/2);
    my $d = $speed * ($time -$speed);
print "$speed: $d\n";
    if ( $dist < $d ) {
        if ($mid != $speed) {
            $mid = $speed;
        } else {
            $mid = $high;
        }
    } else {
        $high = $speed
    }
}


$totalscore = $high - $low - 1;
print "\n== $totalscore\n";
