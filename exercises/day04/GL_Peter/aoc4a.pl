my $totalscore = 0;

my @map = ();
my $lineNum = 0;
my $rowNum = 0;
my @window = ([-1,-1],[0,-1],[1,-1],[-1,0],[1,0],[-1,1],[0,1],[1,1]);

while (<>) {
	chop();
    $map[$lineNum] = [ split('',$_) ];
    $lineNum++;
    $rowNum = length($_);
}
for (my $line=0 ; $line<$lineNum ; $line++) {
    for (my $row=0 ; $row<$rowNum ; $row++) {
        my $rolls = 0;
        if ($map[$line][$row] eq "@") {
           for my $w ( @window ) {
                if($line+$w->[0]>=0 && $line+$w->[0]<$lineNum && $row+$w->[1]>=0 && $row+$w->[1]<$rowNum) {
                    $map[$line+$w->[0]][$row+$w->[1]] ne "." && $rolls++;
                    ($rolls>3) && last;
                }
            }
            if ($rolls < 4) {
                $totalscore++;
                $map[$line][$row] = "x";
            }
        }
        print  $map[$line][$row];
    }
    print "\n";
}

print "\n== $totalscore\n";
