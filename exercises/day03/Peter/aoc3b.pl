use List::Util qw(min);
use List::Util qw(max);

my $totalscore = 0;

my @matrix = ();
my $lineNum = 0;
my $rowNum = 0;
my $gearLst = ();

while (<>) {
	chop();
    $matrix[$lineNum] = [ split('',$_) ];
    $lineNum++;
    $rowNum = length($_);

}

for (my $line=0 ; $line<$lineNum ; $line++) {
    for (my $row=0 ; $row<$rowNum ; $row++) {
        my $num = 0;
        my @starPos = ();
        while ( $matrix[$line][$row] =~ m/\d/) {
            $num = ($num * 10) + $matrix[$line][$row];
#print "$line: ".max(0,$line-1)." ".min($lineNum-1,$line+1)."\n";
            for (my $l=max(0,$line-1) ; $l<=min($lineNum-1,$line+1) ; $l++) {
                for (my $r=max(0,$row-1) ; $r<=min($rowNum-1,$row+1) ; $r++) {
                    if  ($matrix[$l][$r] =~ m/\*/ ) {
                        if (!grep(@starPos,"$l,$r")) {
                            push(@starPos,"$l,$r");
                        }
                    }
                }
            }
            $row++;
        }
        if ($num and $#starPos>=0) {
print "$num: ";
            foreach $star ( @starPos ) {
print " $star ";
                push(@{$starLst{$star}}, $num);
            }
        }
        if ($num and $active) {
            $totalscore += $num;
        }
    }
    print "\n";
}

while (($key, $value) = each (%starLst)) {
    print "$key (".$#{$value}."): ";
    if ( $#{$value} == 1 ) {
        $prod = 1;
        for $v ( @{$value}) {
            $prod *= $v;
        }
        $totalscore += $prod;
    }
    print "\n";
}

print "\n== $totalscore\n";
