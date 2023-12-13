
my $totalscore = 0;

my @timeLst = {};
my @distLst = {};


while (<>) {
	chop();
    if ( $_ =~ /Time: +(\d[ \d]+\d)/ ) {
        @timeLst = split(/ +/, $1) ;
    } elsif ( $_ =~ /Distance: +(\d[ \d]+\d)/ ) {
        @distLst = split(/ +/, $1);
    } 
}

for (my $race=0 ; $race<=$#timeLst ; $race++) {
#print "$timeLst[$race]:\n";
    my $wins = 0;
    for (my $speed=1 ; $speed < $timeLst[$race]; $speed++ ) {
        my $dist = $speed * ($timeLst[$race] -$speed);
        ($dist > $distLst[$race]) and $wins++;
#        print " $speed: $dist ($wins)\n";
    }
    if ($wins) {
        if ($totalscore) {
            $totalscore *= $wins;
        } else {
            $totalscore = $wins;
        }
    }
}


print "\n== $totalscore\n";
