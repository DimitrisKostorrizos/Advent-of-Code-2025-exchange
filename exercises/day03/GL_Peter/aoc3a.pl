use List::Util qw( min max );

my $totalscore = 0;
my $pos = 50;
while (<>) {
	chop();
# split the string $_ into an array of charcters
  my $vaL=0;  
  my @bank = split //, $_;
  my $max = 0;
  my $pos1, $pos2;
  for (my $i = 0; $i + 1 < @bank; $i++) {
    if ($bank[$i] > $max) {
      $pos1 = $i;
      $max = $bank[$i];
    }
  }
  $max = 0;
  for (my $i = $pos1+1; $i < @bank; $i++) {
    if ($bank[$i] > $max) {
      $pos2 = $i;
      $max = $bank[$i];
    }
  }
  $totalscore += $val;
}

print "\n== $totalscore\n";
